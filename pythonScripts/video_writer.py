#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sun Sep 29 13:39:21 2019

@author: msaini
"""



import argparse
import cv2
from imutils import paths
import imageio


ap = argparse.ArgumentParser()
ap.add_argument("-d", "--dataset", required=True,
	help="first input image")
ap.add_argument("-a", "--actionname", required=True,
                help="save_video")
args = vars(ap.parse_args())

imagePaths = sorted(list(paths.list_images(args["dataset"])))

writer = imageio.get_writer(args["actionname"], fps = 5)
for (i, imagePath) in enumerate(imagePaths):
    imageB = cv2.imread(imagePath)
    writer.append_data(imageB)
writer.close()
