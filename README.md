# DetectTO - Object Detector for Android uisng Yolo Models and Onnxruntime 🔎


DetectTO – A simple Android object detection app that lets users select a folder of `.onnx` models, choose one, and run detection on images. Results are saved in `Gallery/DetectTO`.

![Kotlin](https://img.shields.io/badge/Kotlin-blue?style=flat-square&logo=kotlin&logoColor=white)
![Android SDK Min 24](https://img.shields.io/badge/Android%20SDK%20Min-24-green?style=flat-square&logo=android&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-IDE-3DDC84?style=flat-square&logo=androidstudio&logoColor=white)
![YOLO ONNX](https://img.shields.io/badge/YOLO%20ONNX-v11m-lightblue?style=flat-square&logo=ultralytics&logoColor=white)
![ONNX Runtime](https://img.shields.io/badge/ONNX%20Runtime-Android-005CED?style=flat-square&logo=onnx&logoColor=white)


## Limitations

- This app has only been **tested with `YOLOv11m.onnx`** and only YOLO models. (Updated: Other YOLO Models also work well with the apk)
- Performance may vary depending on device hardware and model size.  
- This app is intended as a **demo/testing tool**, not a production-ready solution.
- **Custom label files have not been tested yet**, *You can also test with your own trained model with your custom labels.*
- the default `coco 80 labels` for pretrained models.

## 📌 Note:
*APK is provided. Check out in [Source Codes/apks](https://github.com/John-Da/DetectTO/tree/main/source_codes/apks) (Use at your own risk.)*
*You can contribute to this apk for better performance or fixing issues*

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
*If you wish to use Pretrained YOLO ONNX format, export the model with default settings or you can adjust it as prefered:*
*Noted that if the exported onnx doesn't work, try changing opset*


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

- *FYI: With `Pretrained YOLO11m.onnx`, it takes about ~3s.*

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

