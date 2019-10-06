#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sat Oct  5 22:06:11 2019
# python image_diff.py --first original_01.png --second modified_01.png  -result result.png
@author: msaini
"""

import cv2
import argparse

from matplotlib import pyplot as plt


def save_annotataed_image(fullFile, visualFile, annotatedFile):
    img_rgb = cv2.imread(fullFile)
    img_rgb.shape

    #img_rgb = cv2.resize(img_rgb, (420, 36))
    img_rgb.shape

    template = cv2.imread(visualFile)
    #template = cv2.resize(template, (img_rgb.shape[1], img_rgb.shape[0]))
    height, width,_ = template.shape


    res = cv2.matchTemplate(cv2.cvtColor(img_rgb, cv2.COLOR_BGR2GRAY),cv2.cvtColor(template, cv2.COLOR_BGR2GRAY),cv2.TM_CCOEFF_NORMED)

    # Grab the Max anMin values, plus their locations
    min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(res)

    top_left = max_loc
    bottom_right = (top_left[0] + width, top_left[1] + height)

    cv2.rectangle(img_rgb,top_left, bottom_right, 255, 10)


    plt.imshow(img_rgb)
    cv2.imwrite(annotatedFile, img_rgb)


ap = argparse.ArgumentParser()
ap.add_argument("-f", "--first", required=True,
                help="first input image")
ap.add_argument("-s", "--second", required=True,
                help="second")
ap.add_argument("-r", "--result", required=True,
                help="result")

args = vars(ap.parse_args())
save_annotataed_image(args["first"], args["second"], args["result"])





