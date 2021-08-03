# Universal StarDist for QuPath
Transfer trained StarDist models across imaging modalities
## Introduction
[StarDist](https://github.com/stardist/stardist) is a convolutional neural network (CNN) for cell segmentation, with notable benefits over conventional watershed segmentations. In their repository, they include [several pretrained models for 2D image segmentation](https://github.com/stardist/stardist-imagej/tree/master/src/main/resources/models/2D). Currently, these pretrained models can only be used on images of the same modality that they were trained on. For example, `dsb2018_heavy_augment` was trained on single channel grayscale images of the DAPI stain, and `he_heavy_augment` was trained on a 3 channel RGB H&E stained section. No publicly available models exist for segmenting HDAB or IMC images.

Here, we present a "Universal StarDist" groovy script for QuPath, that features a combination of stain deconvolution, image normalization, and other preprocessing techniques enabling the use of several pretrained models for segmenting images of a different modality. In the figure below, I used the `dsb2018_heavy_augment` with the same parameters to segment an H&E, HDAB, IF, and IMC image:
![Cover Figure](https://user-images.githubusercontent.com/52012166/128095821-eb2e35ea-aff9-4b2c-b36b-3386c2b48e3a.png)
*Even the Hubble space telescope image was segmented using the exact same parameters!*

The preprocessing tools present in our script provides a layer of robustness to StarDist, such that even though a pretrained model might not exist for your specific image type, you have the ability to experiment with publicly available models generated from other imaging modalities. This can save you hours from having to train a model yourself, assuming you have the computational resources at your disposal.

## Installation
Follow the steps listed in [StarDist - QuPath 0.2.3 documentation](https://qupath.readthedocs.io/en/stable/docs/advanced/stardist.html) to build QuPath 0.2.3 with TensorFlow. This includes [downloading some pretrained models](https://github.com/stardist/stardist-imagej/tree/master/src/main/resources/models/2D). Verify that StarDist is working by progressing through QuPath's documentation and running on a few sample images.

Download `Multimodal StarDist Segmentation.groovy` from this repository. Set `pathModel` in lines 30-33 to the pretrained brightfield and IF models listed above. Even if you plan to use only one model, it's still a good idea to modify the commented-out paths should you choose to change your model. Also remember, you need to have a selected annotation to run the StarDist segmentation. If you want to segment the whole image (not advised), use `Create full image annotation` from QuPath's objects dropdown menu, or via scripting.

Set the variables listed under `Variables to set` accordingly. Read the comments adjacent to them for what they do.

**IMPORTANT**: NOT ALL MODEL-IMAGE COMBINATIONS WILL WORK! You can't use a model trained on 3 channels (i.e. H&E) to segment a 1 channel image (i.e. IF). I've tried to catch these errors, and if the segmentation does fail, the log should provide a descriptive error.

| Model (column) vs Image (row)  | 1 channel (`dsb2018_paper` or `dsb2018_heavy_augment`) |3 channels (`he_heavy_augment` ) |
| ------------- | ------------- | ------------- |
| **IF**  | Yes  | No  |
| **IMC**  | Yes (enable normalization, 1-99th percentile works)  | No  |
| **HDAB**  | Yes (set color vectors, enable normalization)  | Yes (segmentation may be poor if brown nuclei are too optically dense)  |
| **H&E**  | Yes (set color vectors, enable normalization)  | Yes  |
