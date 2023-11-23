# Universal StarDist for QuPath
### Transfer trained StarDist models across imaging modalities with ease!
## Preamble
Universal Stardist for QuPath is a groovy script for use with the [QuPath Stardist extension](https://github.com/qupath/qupath-extension-stardist) developed by Dr. Pete Bankhead. Scripts taken from the [QuPath documentation on StarDist] (https://qupath.readthedocs.io/en/0.4/docs/deep/stardist.html) were aggregated and repurposed to allow for transmodality image segmentation. By detecting the modality of the image (Brightfield, HDAB, IF, etc.) from the image metadata, various preprocessing steps are applied to transform the input image to mimic the modality of the model used in training, effectively allowing for StarDist models trained in one modality to be applied to another. 

For example, if an H&E image is segmented with an IF model (e.g. DSB2018), stain deconvolution is applied to the hematoxylin channel, which is then normalized, and passed to StarDist for segmentation, effectively passing a pseudo-IF DAPI image. For IMC, only normalization is applied however the channel index of the DNA counterstain must be specified. A summary of model-image compatibility is described in `Model-modality compatibility`.

This script would not be possible without the contributions of Dr. Pete Bankhead and associated developers in making QuPath and the StarDist extension, and Dr. Uwe Schmidt's contributions for StarDist. As such, it is imperative to cite the aforementioned StarDist extension, and the publications listed under `Citing`.
## Introduction
[StarDist](https://github.com/stardist/stardist) is a convolutional neural network (CNN) for cell segmentation, with notable benefits over conventional watershed segmentations. In their repository, they include [several pretrained models for 2D image segmentation](https://github.com/stardist/stardist-imagej/tree/master/src/main/resources/models/2D). Currently, these pretrained models can only be used on images of the same modality that they were trained on. For example, `dsb2018_heavy_augment` was trained on single channel grayscale images of the DAPI stain, and `he_heavy_augment` was trained on a 3 channel RGB H&E stained section. No publicly available models exist for segmenting HDAB or IMC images.

Here, we present a "Universal StarDist" groovy script for QuPath, that features a combination of stain deconvolution, image normalization, and other preprocessing techniques enabling the use of several pretrained models for segmenting images of a different modality. In the figure below, I used the `dsb2018_heavy_augment` with the same parameters to segment an H&E, HDAB, IF, and IMC image:
![Cover Figure](https://user-images.githubusercontent.com/52012166/128095821-eb2e35ea-aff9-4b2c-b36b-3386c2b48e3a.png)
*Even the Hubble space telescope image was segmented using the exact same parameters!*

The preprocessing tools present in our script provides a layer of robustness to StarDist, such that even though a pretrained model might not exist for your specific image type, you have the ability to experiment with publicly available models generated from other imaging modalities. This can save you hours from having to train a model yourself, assuming you have the computational resources at your disposal.

## Installation
Follow the steps listed in https://qupath.readthedocs.io/en/stable/docs/deep/stardist.html to setup StarDist with QuPath 0.3.0. This includes [downloading some pretrained models](https://github.com/stardist/stardist-imagej/tree/master/src/main/resources/models/2D). Verify that StarDist is working by progressing through QuPath's documentation and running on a few sample images. If using QuPath 0.2.3, a legacy script has been included as the library for StarDist changes in 0.3.0. With the release of 0.3.0, Universal-StarDist-for-QuPath supports Tensorflow (CPU), OpenCV (CPU), and OpenCV (GPU). GPU-accelerated segmentation can be 47-223% faster, depending on your CPU, GPU, pre/post processing steps in the StarDist builder, and RAM/VRAM.

Download `Multimodal StarDist Segmentation.groovy` from this repository. Set `pathModel` in lines 30-33 to the pretrained brightfield and IF models listed above. Even if you plan to use only one model, it's still a good idea to modify the commented-out paths should you choose to change your model. Also remember, you need to have a selected annotation to run the StarDist segmentation. If you want to segment the whole image (not advised), use `Create full image annotation` from QuPath's objects dropdown menu, or via scripting.

Set the variables listed under `Variables to set` accordingly. Read the comments adjacent to them for what they do.
### Variable description
- `model_trained_on_single_channel`: Set to 1 if the pretrained model you're using was trained on IF sections, set to 0 if trained on brightfield
- `param_channel`: channel to use for nucleus detection. First channel in image is channel 1. If working with H&E or HDAB, channel 1 is hematoxylin.
- `param_median`: median preprocessing: Requires an int value corresponding to the radius of the median filter kernel. For radii larger than 2, the image must be in uint8 bit depth. Default 0
- `param_divide`: division preprocessing: int or floating point, divides selected channel intensity by value before segmenting. Useful when normalization is disabled. Default 1
- `param_threshold`: threshold for detection. All cells segmented by StarDist will have a detection probability associated with it, where higher values indicate more certain detections. Floating point, range is 0 to 1. Default 0.5
- `param_pixelsize`: size of tile in pixels for processing. Must be a multiple of 16. Lower values may solve any memory-related errors, but can take longer to process. Default is 1024.
- `param_expansion`: size of cell expansion in pixels. Default is 10.
- `min_nuc_area`: remove detections with `nuc_area_measurement` less than or equal to this value. Default is 0.
- `nuc_area_measurement`: area measurement for object filtering. Default is "Nucleus: Area µm^2"
- `min_nuc_intensity`: remove detections with `nuc_intensity_measurement` less than or equal to this value. Default is 0.
- `nuc_intensity_measurement`: intensity measurement for object filtering.
- `normalize_low_pct`: lower limit for normalization. Set to 0 to disable
- `normalize_high_pct`: upper limit for normalization. Set to 100 to disable.
## Model-modality compatibility
**IMPORTANT**: NOT ALL MODEL-IMAGE COMBINATIONS WILL WORK! You can't use a model trained on 3 channels (i.e. H&E) to segment a 1 channel image (i.e. IF). I've tried to catch these errors, and if the segmentation does fail, the log should provide a descriptive error.

| Image (row) vs Model (column)  | 1 channel (`dsb2018_paper` or `dsb2018_heavy_augment`) |3 channels (`he_heavy_augment` ) |
| ------------- | ------------- | ------------- |
| **IF**  | Yes  | No  |
| **IMC**  | Yes (enable normalization, 1-99th percentile works)  | No  |
| **HDAB**  | Yes (set color vectors, enable normalization)  | Yes (segmentation may be poor if brown nuclei are too optically dense)  |
| **H&E**  | Yes (set color vectors, enable normalization)  | Yes  |

## Citing
Please cite this repository:
```
Zaidi M., McKee T., Wouters B. (2021). Universal-StarDist-for-QuPath (https://github.com/MarkZaidi/Universal-StarDist-for-QuPath/) (version 1.0.0). Date released: 2021-08-03
```
StarDist:
```
Uwe Schmidt, Martin Weigert, Coleman Broaddus, and Gene Myers. Cell Detection with Star-convex Polygons. International Conference on Medical Image Computing and Computer-Assisted Intervention (MICCAI), Granada, Spain, September 2018.
```
And QuPath:
```
Bankhead, P. et al. (2017). QuPath: Open source software for digital pathology image analysis. Scientific Reports. https://doi.org/10.1038/s41598-017-17204-5
```
## To do:
- Fix normalization issues
  - Ideally, I want to have an if statement embedded in the StarDist builder. Something along the lines of ``` If !(normalize_low_pct==0 & normalize_high_pct==100): .normalizePercentiles(normalize_low_pct,normalize_high_pct)``` such that setting them to 0 and 100 respectively disables `.normalizePercentiles` from being included in the builder, entirely. Just having trouble with making `if` statements inside of builders...
- move `def pathObjects = getSelectedObjects()` to the top
- Add functionality for summing multiple channels as a preprocessing step. Specifically for HDAB: Deconvolve, sum hematoxylin and DAB channels, and normalize). Gives more of a robust segmentation when you have intense DAB nuclei that mask the hematoxylin signal.
