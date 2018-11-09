package org.vaadin.johannest.tf;

import javafx.util.Pair;
import org.tensorflow.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class TensorFlowPredictor {

    private static final String modelFileName = "tensorflow_inception_graph.pb";
    private static final String labelsFileName = "imagenet_comp_graph_label_strings.txt";
    private List<String> labels = new ArrayList<>();
    private byte[] graphDef;

    public TensorFlowPredictor() {
        readModelGraph();
        readClassLabels();
    }

    private void readModelGraph() {
        String modelPath = getFilesPath(modelFileName);
        graphDef = readAllBytesOrExit(Paths.get(modelPath.startsWith("/C") ? modelPath.substring(1) : modelPath));
    }

    private static byte[] readAllBytesOrExit(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println(
                    "Failed to read [" + path + "]: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    private void readClassLabels() {
        String labelsPath = getFilesPath(labelsFileName);
        try (Stream<String> stream = Files.lines(Paths.get(labelsPath.startsWith("/C") ? labelsPath.substring(1) : labelsPath))) {
            stream.forEach(labels::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilesPath(String filename) {
        return getClass().getResource("/META-INF/resources/tf/models/" + filename).getFile();
    }

    /**
     * Predict class of jpeg imageBytes, return kBest results
     *
     * @param imageBytes jpeg bytes
     * @param kBest classifications
     * @return
     */
    public List<Pair<String,Float>> predictClass(byte[] imageBytes, int kBest) {
        List<Pair<String,Float>> resultList = new ArrayList<>();
        String bestLabel = "unknown";
        Float labelProb = -1f;

        if (imageBytes!=null && imageBytes.length>100) {

            Tensor imageTensor = constructAndExecuteGraphToNormalizeImage(imageBytes);
            float[] classProbabilities = executeGraphForTensor(imageTensor);

            int bestLabelIdx = maxIndex(classProbabilities);
            labelProb = roundToScale(classProbabilities[bestLabelIdx],3);
            bestLabel = labels.get(bestLabelIdx);

            resultList.add(new Pair<>(bestLabel, labelProb));

            for (int k=1; k<kBest; k++) {
                int kThIndex = findKthLargestIndex(classProbabilities,k+1);
                resultList.add(new Pair<>(labels.get(kThIndex), roundToScale(classProbabilities[kThIndex],3)));
            }
        }
        return resultList;
    }

    private float roundToScale(float classProbability, int scale) {
        return new BigDecimal(
                classProbability * 100f)
                .setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private static Tensor constructAndExecuteGraphToNormalizeImage(
            byte[] imageBytes) {
        try (Graph normalizationGraph = new Graph()) {
            GraphBuilder graphBuilder = new GraphBuilder(normalizationGraph);
            // Some constants specific to the pre-trained model at:
            // https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
            //
            // - The model was trained with images scaled to 224x224 pixels.
            // - The colors, represented as R, G, B in 1-byte each were
            // converted to
            // float using (value - Mean)/Scale.
            final int height = 224;
            final int width = 224;
            final float mean = 117f;
            final float scale = 1f;

            // Since the graph is being constructed once per execution here, we
            // can use a constant for the
            // input image. If the graph were to be re-used for multiple input
            // images, a placeholder would
            // have been more appropriate.
            final Output input = graphBuilder.constant("input", imageBytes);
            final Output output = graphBuilder.div(
                    graphBuilder.sub(
                            graphBuilder.resizeBilinear(
                                    graphBuilder.expandDims(
                                            graphBuilder.cast(graphBuilder
                                                    .decodeJpeg(input, 3),
                                                    DataType.FLOAT),
                                            graphBuilder.constant("make_batch",
                                                    0)),
                                    graphBuilder.constant("size",
                                            new int[] { height, width })),
                            graphBuilder.constant("mean", mean)),
                    graphBuilder.constant("scale", scale));
            try (Session session = new Session(normalizationGraph)) {
                return session.runner().fetch(output.op().name()).run().get(0);
            }
        }
    }

    private float[] executeGraphForTensor(Tensor imageTensor) {
        try (Graph graph = new Graph()) {
            graph.importGraphDef(graphDef);
            try (Session session = new Session(graph);
                 Tensor result = session.runner().feed("input", imageTensor)
                         .fetch("output").run().get(0)) {
                final long[] rshape = result.shape();
                if (result.numDimensions() != 2 || rshape[0] != 1) {
                    throw new RuntimeException(String.format(
                            "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                            Arrays.toString(rshape)));
                }
                int nlabels = (int) rshape[1];
                float[][] floats = new float[1][nlabels];
                result.copyTo(floats);
                return floats[0];
            }
        }
    }

    private static int maxIndex(float[] probabilities) {
        int best = 0;
        for (int i = 1; i < probabilities.length; ++i) {
            if (probabilities[i] > probabilities[best]) {
                best = i;
            }
        }
        return best;
    }

    private int findKthLargestIndex(float[] arr, int k) {
        List<Pair<Float,Integer>> list = new ArrayList<>();
        for (int i=0; i<arr.length; i++) {
            list.add(new Pair<>(arr[i],i));
        }
        list.sort(Comparator.comparing(Pair::getKey));
        int targetIndex = arr.length - k;
        return list.get(targetIndex).getValue();
    }

    // In the fullness of time, equivalents of the methods of this class should
    // be auto-generated from
    // the OpDefs linked into libtensorflow_jni.so. That would match what is
    // done in other languages
    // like Python, C++ and Go.
    static class GraphBuilder {
        GraphBuilder(Graph g) {
            this.g = g;
        }

        Output div(Output x, Output y) {
            return binaryOp("Div", x, y);
        }

        Output sub(Output x, Output y) {
            return binaryOp("Sub", x, y);
        }

        Output resizeBilinear(Output images, Output size) {
            return binaryOp("ResizeBilinear", images, size);
        }

        Output expandDims(Output input, Output dim) {
            return binaryOp("ExpandDims", input, dim);
        }

        Output cast(Output value, DataType dtype) {
            return g.opBuilder("Cast", "Cast").addInput(value)
                    .setAttr("DstT", dtype).build().output(0);
        }

        Output decodeJpeg(Output contents, long channels) {
            return g.opBuilder("DecodeJpeg", "DecodeJpeg").addInput(contents)
                    .setAttr("channels", channels).build().output(0);
        }

        Output decodePng(Output contents, long channels) {
            return g.opBuilder("DecodePng", "DecodePng").addInput(contents)
                    .setAttr("channels", channels).build().output(0);
        }

        Output constant(String name, Object value) {
            try (Tensor t = Tensor.create(value)) {
                return g.opBuilder("Const", name).setAttr("dtype", t.dataType())
                        .setAttr("value", t).build().output(0);
            }
        }

        private Output binaryOp(String type, Output in1, Output in2) {
            return g.opBuilder(type, type).addInput(in1).addInput(in2).build()
                    .output(0);
        }

        private Graph g;
    }

}
