# Universal StarDist for QuPath
Transfer trained StarDist models across imaging modalities
## Introduction
[StarDist](https://github.com/stardist/stardist) is a convolutional neural network (CNN) for cell segmentation, with notable benefits over conventional watershed segmentations. In their repository, they include [several pretrained models for 2D image segmentation](https://github.com/stardist/stardist-imagej/tree/master/src/main/resources/models/2D). Currently, these pretrained models can only be used on images of the same modality that they were trained on. For example, `dsb2018_heavy_augment` was trained on single channel grayscale images of the DAPI stain, and `he_heavy_augment` was trained on a 3 channel RGB H&E stained section. No publicly available models exist for segmenting HDAB or IMC images.

Here, we present a "Universal StarDist" groovy script for QuPath, that features a combination of stain deconvolution, image normalization, and other preprocessing techniques enabling the use of several pretrained models for segmenting images of a different modality. In the figure below, I used the `dsb2018_heavy_augment` with the same parameters to segment an H&E, HDAB, IF, and IMC image:
