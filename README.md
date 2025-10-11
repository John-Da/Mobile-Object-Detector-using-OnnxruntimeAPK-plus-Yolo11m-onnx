# DetectTO


DetectTO – A simple Android object detection app that lets users select a folder of `.onnx` models, choose one, and run detection on images. Results are saved in `Gallery/DetectTO`.

## Limitations

- This app has only been **tested with `YOLOv11m.onnx`**.  
- Other ONNX models may **not work correctly**.  
- Performance may vary depending on device hardware and model size.  
- This app is intended as a **demo/testing tool**, not a production-ready solution.
- Custom Labels is **not tested yet.**

## ⚠️ Warning:
*APK is provided for testing purposes. Use at your own risk. Check out in [Source Code/apks](https://github.com/John-Da/DetectTO/tree/main/source_codes/apks)*

## ⚙️🛠️ How to use

( *Portrait Screen is prefered* )

- Download the apk from [Source Code/apks](https://github.com/John-Da/DetectTO/tree/main/source_codes/apks)
- If your device prevents unknown app installation, allow it. ( You can turn it off anytime ).
- Create folder in your storage and put your `model.onnx` files in it.
- Open the app and select the folder you created.  
- BOOM! You’re good to go!  
- Select Model → Select Image from Gallery / Take a photo → Confirm → View/Save Results!
  
♦️ => *You can change IOU, Label Size and Image W/H is based on the Onnx model that you exported with.* <=

## 💡 Tip:
*If you wish to use YOLO with ONNX, export the model as default for this demo project:*


```python
from ultralytics import YOLO
 
# Load a model
model = YOLO("yolo11m.pt")        # Load an official model
# or
model = YOLO("path/to/best.pt")   # Load a custom trained model

# Export the model to ONNX
model.export(format="onnx")
```

## 📌 News:
*There is another similar project: Mobile Web-Based App, using React Native (client) and Flask Sever (host). See more in [this repo](https://github.com/John-Da/DetectTO-Mobile-WebBased-App)*

## License

This project is licensed under the MIT License.  
See the [license](https://github.com/John-Da/DetectTO/blob/main/LICENSE) file for details.

