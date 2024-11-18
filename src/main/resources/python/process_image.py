import cv2
import numpy as np
import sys
import json
import base64

def process_image(image_path):
    # Lee la imagen
    frame = cv2.imread(image_path)
    
    # Convierte a HSV
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    
    # Define los rangos de colores  
    color_ranges = {
        'blue': ([100, 150, 50], [140, 255, 255]),
        'green': ([40, 50, 50], [80, 255, 255]),
        'red': ([0, 120, 70], [10, 255, 255]),
        'yellow': ([20, 150, 150], [30, 255, 255])
    }
    
    results = {}
    
    for color_name, (lower, upper) in color_ranges.items():
        # Crear máscara para el color
        mask = cv2.inRange(hsv, np.array(lower), np.array(upper))
        
        if color_name == 'red':
            # Añadir segundo rango para rojo
            lower_red2 = np.array([170, 120, 70])
            upper_red2 = np.array([180, 255, 255])
            mask2 = cv2.inRange(hsv, lower_red2, upper_red2)
            mask = cv2.bitwise_or(mask, mask2)
        
        # Detectar círculos
        blurred = cv2.GaussianBlur(mask, (9, 9), 2)
        circles = cv2.HoughCircles(blurred, cv2.HOUGH_GRADIENT, dp=1, minDist=50,
                                 param1=50, param2=30, minRadius=20, maxRadius=100)
        
        if circles is not None:
            circles = np.uint16(np.around(circles))
            results[color_name] = {
                'detected': True,
                'count': len(circles[0]),
                'positions': [[int(x), int(y), int(r)] for x, y, r in circles[0]]
            }
        else:
            results[color_name] = {
                'detected': False,
                'count': 0,
                'positions': []
            }
    
    # Guardar imagen procesada
    cv2.imwrite('output.png', frame)
    
    return results

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Error: No image path provided")
        sys.exit(1)
        
    image_path = sys.argv[1]
    results = process_image(image_path)
    print(json.dumps(results))