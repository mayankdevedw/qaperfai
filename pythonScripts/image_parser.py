#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sun Sep 29 13:39:21 2019

@author: msaini
"""



import argparse
import cv2
import imutils
import matplotlib.pyplot as plt
import numpy as np
import os
from imutils import paths
from skimage.measure import compare_ssim


def display(img,cmap='gray'):
    fig = plt.figure(figsize=(12,10))
    ax = fig.add_subplot(111)
    ax.imshow(img,cmap='gray')




def mse(imageA, imageB):
    # the 'Mean Squared Error' between the two images is the
    # sum of the squared difference between the two images;
    # NOTE: the two images must have the same dimension
    err = np.sum((imageA.astype("float") - imageB.astype("float")) ** 2)
    err /= float(imageA.shape[0] * imageA.shape[1])
    return '%.3f' % err

def compare_image_with_abs_diff(imageA, imageB):
    grayA = cv2.cvtColor(imageA, cv2.COLOR_BGR2GRAY)
    grayB = cv2.cvtColor(imageB, cv2.COLOR_BGR2GRAY)
    (score, _) = compare_ssim(grayA, grayB, full=True)
    print("SSIM: {}".format(score))
    diff = cv2.absdiff(grayA, grayB)
    diff = cv2.GaussianBlur(diff, (3, 3), 0)
    edges = cv2.Canny(diff, 100, 200)

    _, thresh = cv2.threshold(edges, 0, 255, cv2.THRESH_BINARY)
    cnts = cv2.findContours(thresh.copy(), cv2.RETR_EXTERNAL,
                            cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)
    for c in cnts:
        (x, y, w, h) = cv2.boundingRect(c)
        cv2.rectangle(imageA, (x, y), (x + w, y + h), (0, 0, 255), 2)
        cv2.rectangle(imageB, (x, y), (x + w, y + h), (0, 0, 255), 2)
    return imageA, imageB



def delete_images_with_zero_mse(imagepaths):
    for (i, imagePath) in enumerate(imagePaths):
        if i == 0 :
            img_1 = imagePath
            continue;
        print("[INFO] Comparing image {} with {}".format(img_1,imagePath))
        imageA = cv2.imread(img_1)
        imageB = cv2.imread(imagePath)
        grayA = cv2.cvtColor(imageA, cv2.COLOR_BGR2GRAY)
        grayB = cv2.cvtColor(imageB, cv2.COLOR_BGR2GRAY)

        err = mse(grayA, grayB)
        (score, _) = compare_ssim(grayA, grayB, full=True)
        print ("mse: %s, ssim: %s" % (err, score))
        if float(err) == 0.000:
            print("[INFO] Deleteing image {}".format(img_1))
            os.remove(img_1)
        else:
            break
        img_1 = imagePath


ap = argparse.ArgumentParser()
ap.add_argument("-d", "--dataset", required=True,
	help="first input image")
args = vars(ap.parse_args())

imagePaths = sorted(list(paths.list_images(args["dataset"])), reverse=True)



delete_images_with_zero_mse(imagePaths)
imagePaths.clear()

imagePaths = sorted(list(paths.list_images(args["dataset"])))



for (i, imagePath) in enumerate(imagePaths):

    if i == 0 :
        img_1 = imagePath
        continue
    print("[INFO] Comparing image {} with {}".format(img_1,imagePath))

    imageA = cv2.imread(img_1)
    imageB = cv2.imread(imagePath)
    imageA, imageB = compare_image_with_abs_diff(imageA, imageB)
    cv2.imwrite(img_1, imageB)
    #cv2.imwrite(imagePath, imageB)
    img_1 = imagePath
