# DetectTO - OnnxRuntime Android Object Detector 🔎


DetectTO – A simple Android object detection app that lets users select a folder of `.onnx` models, choose one, and run detection on images. Results are saved in `Gallery/DetectTO`.

<p>
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-blue?logo=kotlin&logoColor=white" height="35"/>
  &nbsp;
  <img alt="Android SDK Min Version" src="https://img.shields.io/badge/AndroidSDK%20Min-24-green?logo=android&logoColor=white" height="35"/>
  &nbsp;
  <img alt="Android Studio IDE" src="https://img.shields.io/badge/Android%20Studio-IDE-black?logo=androidstudio&logoColor=white" height="35"/>
  &nbsp;
  <img alt="Yolo 11m" src="https://img.shields.io/badge/Yolo%20Onnx-v11m-lightblue?logo=yolo&logoColor=white" height="35"/>
  &nbsp;
  <img alt="ONNXRUNTIME" src="https://img.shields.io/badge/ONNXRUNTIME-Android-005CED?logo=ONNX&logoColor=white" height="35"/>
</p>


## Limitations

- This app has only been **tested with `YOLOv11m.onnx`**.  
- Other ONNX models may **not work correctly**.  
- Performance may vary depending on device hardware and model size.  
- This app is intended as a **demo/testing tool**, not a production-ready solution.
- **Custom label files have not been tested yet**; only the default `coco 80 labels` are recommended for this version.
- *FYI: With `Pretrained YOLO11m.onnx`, it takes about ~3s.*

## ⚠️ Warning:
*APK is provided for testing purposes. Use at your own risk. Check out in [Source Codes/apks](https://github.com/John-Da/DetectTO/tree/main/source_codes/apks)*

## ⚙️🛠️ How to use

( *Portrait Screen is prefered* )

- Download the apk from [Source Codes/apks](https://github.com/John-Da/DetectTO/tree/main/source_codes/apks)
- If your device prevents unknown app installation, allow it. ( You can turn it off anytime ).
- Create folder in your storage and put your `model.onnx` files in it.
- Open the app and select the folder you created.  
- BOOM! You’re good to go!  
- Choose Model → Select Image from Gallery / Take a photo → Inputs → Confirm → View/Save Results!
  
♦️ => *You can adjust IOU and label size, but the image width and height should match those of the ONNX model you exported. (mostly img w/h = exported onnx's imgsz)* <=

## 💡 Tip:
*If you wish to use YOLO ONNX format as the demo, export the model as default for this demo project:*


```python
from ultralytics import YOLO
 
# Load a model
model = YOLO("yolo11m.pt")        # Load an official model
# or
model = YOLO("path/to/best.pt")   # Load a custom trained model

# Export the model to ONNX
model.export(format="onnx")
```

## 📲 Demo:

https://github.com/user-attachments/assets/82057093-a032-4852-9827-78e029e22f98


## 📌 News:
*There is another similar project: Mobile Web-Based App, using React Native (client) and Flask Sever (host). See more in [this repo](https://github.com/John-Da/DetectTO-Mobile-WebBased-App)*

## License

This project is licensed under the MIT License.  
See the [license](https://github.com/John-Da/DetectTO/blob/main/LICENSE) file for details.

## ⭐️ Acknowledgements  

- **Android Studio IDE for Android development.**
- **ONNX Runtime**
- **YOLO11m Powered by Ultralytics**

