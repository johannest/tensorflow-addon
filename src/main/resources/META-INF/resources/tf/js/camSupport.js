var fps = 10;

// This code is pretty much copy paste with small modifications of
// Apache2 licensed code of https://github.com/elmot/vaadin-10-qr-reader
var jsQRCam = {
    prevCodeData: null,
    drawLine: function (begin, end, color) {
        this.canvas.beginPath();
        this.canvas.moveTo(begin.x, begin.y);
        this.canvas.lineTo(end.x, end.y);
        this.canvas.lineWidth = 4;
        this.canvas.strokeStyle = color;
        this.canvas.stroke();
    },

    tick: function () {
        jsQRCam.loadingMessage.classList.add("loading");
        if (jsQRCam.video.readyState === jsQRCam.video.HAVE_ENOUGH_DATA) {
            jsQRCam.loadingMessage.hidden = true;
            jsQRCam.canvasElement.hidden = false;

            jsQRCam.canvasElement.height = jsQRCam.video.videoHeight;
            jsQRCam.canvasElement.width = jsQRCam.video.videoWidth;
            jsQRCam.canvas.drawImage(jsQRCam.video, 0, 0, jsQRCam.canvasElement.width, jsQRCam.canvasElement.height);
        }

        setTimeout(function() {
            requestAnimationFrame(jsQRCam.tick);
        }, 1000.0 / fps);

    },
    init: function () {
        this.video = document.createElement("video");
        this.canvasElement = document.getElementById("jsQRCamCanvas");
        this.canvas = this.canvasElement.getContext("2d");
        this.loadingMessage = document.getElementById("jsQRCamLoadingMessage");
        // Use facingMode: environment to attemt to get the front camera on phones
        navigator.mediaDevices.getUserMedia({video: {facingMode: "user"}}).then(function (stream) {
            jsQRCam.video.srcObject = stream;
            jsQRCam.video.setAttribute("playsinline", true); // required to tell iOS safari we don't want fullscreen
            jsQRCam.video.play();
            requestAnimationFrame(jsQRCam.tick);
        });
    },
    reset: function () {
        this.prevCodeData = null;
    },
    capture: function() {
        var b64encoded = jsQRCam.canvas.canvas.toDataURL('image/jpeg', 0.8);
        jsQRCam.canvasElement.parentElement.$server.onImage(b64encoded);
    }
};

