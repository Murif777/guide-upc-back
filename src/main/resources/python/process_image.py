import cv2
import numpy as np
import sys
import json
from pyzbar import pyzbar

def process_image(image_path):
    # Read the image
    frame = cv2.imread(image_path)

    # Convert to HSV
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)

    # Detect QR code
    qrdetector = pyzbar.decode(frame)

    # Define color ranges
    color_ranges = {
        'blue': ([100, 150, 50], [140, 255, 255]),
    }

    results = {
        'qr_data': '',
        'message': '',
    }

    # Check for QR code
    if qrdetector:
        qr_data = [obj.data.decode("utf-8") for obj in qrdetector]
        results['qr_data'] = qr_data
        results['message'] = f"{', '.join(qr_data)}"

    # Check for blue color
    for color_name, (lower, upper) in color_ranges.items():
        mask = cv2.inRange(hsv, np.array(lower), np.array(upper))
        if color_name == 'blue' and cv2.countNonZero(mask) > 0:
            results['message'] = 'Blue detected'
            break

    return results

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Error: No image path provided")
        sys.exit(1)

    image_path = sys.argv[1]
    results = process_image(image_path)
    print(json.dumps(results))