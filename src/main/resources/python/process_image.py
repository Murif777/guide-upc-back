import cv2
import numpy as np
import sys
import json
from pyzbar import pyzbar

def process_image(image_path):
    # Leer la imagen
    frame = cv2.imread(image_path)

    # Convertir a HSV
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)

    # Detectar QR code
    qrdetector = pyzbar.decode(frame)

    # Rango de color azul
    lower_blue = np.array([100, 150, 50])
    upper_blue = np.array([140, 255, 255])

    results = {
        'qr_data': [],
        'message': '',
        'lines_detected': False,
    }

    # Detectar códigos QR
    if qrdetector:
        qr_data = [obj.data.decode("utf-8") for obj in qrdetector]
        results['qr_data'] = qr_data
        results['message'] = f"{', '.join(qr_data)}"

    # Crear máscara para azul
    mask = cv2.inRange(hsv, lower_blue, upper_blue)

    # Detectar bordes
    edges = cv2.Canny(mask, 50, 150)

    # Detectar líneas
    lines = cv2.HoughLinesP(edges, rho=1, theta=np.pi / 180, threshold=50, minLineLength=50, maxLineGap=10)

    if lines is not None:
        results['lines_detected'] = True
        results['message'] = 'Blue line detected'

    return results

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Error: No image path provided")
        sys.exit(1)

    image_path = sys.argv[1]
    results = process_image(image_path)
    print(json.dumps(results))
