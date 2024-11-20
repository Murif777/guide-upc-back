import cv2
import numpy as np
import sys
import json
from pyzbar import pyzbar


def process_image(image_path):
    # Lee la imagen
    frame = cv2.imread(image_path)
    
    # Convierte a HSV
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    #detector del qr
    qrdetector= pyzbar.decode(frame)
    
    # Define los rangos de colores  
    color_ranges = {
        'blue': ([100, 150, 50], [140, 255, 255]),
       # 'green': ([40, 50, 50], [80, 255, 255]),
        #'red': ([0, 120, 70], [10, 255, 255]),
        #'yellow': ([20, 100, 100], [30, 255, 255]),
    }
    
    results = {
        'qr_data': '',
        'message': '',
    }
    if qrdetector:
        qr_data = [obj.data.decode("utf-8") for obj in qrdetector]
        results['qr_data'] = qr_data
        print("ESCANEO"+qr_data);
    
    for color_name, (lower, upper) in color_ranges.items():
        # Crear máscara para el color
        mask = cv2.inRange(hsv, np.array(lower), np.array(upper))
        
       # if color_name == 'yellow':
#     # Detectar círculos amarillos
#     blurred = cv2.GaussianBlur(mask, (9, 9), 2)  # Aplicar desenfoque Gaussiano para suavizar la imagen
#     circlesyellow = cv2.HoughCircles(
#         blurred, cv2.HOUGH_GRADIENT, dp=1,
#         minDist=50, param1=50, param2=30, minRadius=20, maxRadius=100
#     )
#     if circlesyellow is not None:
#         results['message'] = 'Estas son las intercepciones'
#         break
#
# if color_name == 'red':
#     # Detectar círculos rojos
#     blurred = cv2.GaussianBlur(mask, (9, 9), 2)  # Aplicar desenfoque Gaussiano para suavizar la imagen
#     circlesred = cv2.HoughCircles(
#         blurred, cv2.HOUGH_GRADIENT, dp=1,
#         minDist=50, param1=50, param2=30, minRadius=20, maxRadius=100
#     )
#     if circlesred is not None:
#         results['message'] = 'Estas son la de las entradas'
#         break
#
# if color_name == 'green':
#     # Detectar círculos verdes
#     blurred = cv2.GaussianBlur(mask, (9, 9), 2)  # Aplicar desenfoque Gaussiano para suavizar la imagen
#     circles = cv2.HoughCircles(
#         blurred, cv2.HOUGH_GRADIENT, dp=1, minDist=50,
#         param1=50, param2=30, minRadius=20, maxRadius=100
#     )
#     if circles is not None:
#         results['message'] = 'Estas son de los puntos importantes'
#         # Aquí se asume que los puntos importantes están relacionados con zonas clave como la biblioteca o bloques importantes.
#         break


        if color_name == 'blue' and cv2.countNonZero(mask) > 0:
            results['message'] = 'Este es el de el camino'
            break

    return results

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Error: No image path provided")
        sys.exit(1)
        
    image_path = sys.argv[1]
    results = process_image(image_path)
    print(json.dumps(results))
