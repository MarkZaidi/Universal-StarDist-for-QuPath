/** Scripts enabling use of pretrained stardist models for nucleus segmentation on Brightfield or IF images
 * 3 pretrained models are available at https://github.com/stardist/stardist-imagej/tree/master/src/main/resources/models/2D
 * and must be downloaded prior to running this script. Furthermore, you need to build QuPath with tensorflow (verified with CPU)
 * using the instructions here: https://qupath.readthedocs.io/en/latest/docs/advanced/stardist.html
 * he_heavy_augment is a H&E-trained model, and requires a 3-channel input (like H&E or HDAB). dsb2018_paper and dsb2018_heavy_augment
 * are IF trained models, and requires a 1-channel input (either an IF nuclear marker like DAPI, or a deconvolved nuclear marker
 * from brightfield images like hematoxylin). This allows pretrained IF models to be used for both IF and brightfield segmentation
 */
//Variables to set *************************************************************
def model_trained_on_single_channel=1 //Set to 1 if the pretrained model you're using was trained on IF sections, set to 0 if trained on brightfield
param_channel=1 //channel to use for deconvolution. Note, the 1st channel is counted as number 0, and so on
param_median=0 //median filter preprocessing
param_divide=1 //division preprocessing
param_add=0 //addition preprocessing
param_threshold = 0.5//threshold for deteciton
param_pixelsize=0 //resolution to perform segmentation at. Set to 0 for image resolution
param_tilesize=1024 //size of tile for processing
param_expansion=10 //size of cell expansion
def min_nuc_area=0 //remove any nuclei with an area less than this (in microns)
nuc_area_measurement='Nucleus: Area µm^2'
def min_nuc_intensity=0 //remove any detections with an intensity below this value
nuc_intensity_measurement='Ir(193)_193Ir-DNA193: Nucleus: Mean'
normalize_low_pct=0 //lower limit for normalization. Set to 0 to disable
normalize_high_pct=100 // upper limit for normalization. Set to 100 to disable.

// Specify the model directory (you will need to change this!). Uncomment the model you wish to use
//Brightfield models
    //def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/he_heavy_augment'
// IF models
    //def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/dsb2018_paper'
    def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/dsb2018_heavy_augment'

//End of variables to set ******************************************************
param_channel=param_channel-1 // corrects for off-by-one error
normalize_high_pct=normalize_high_pct-0.000000000001 //corrects for some bizarre normalization issue when attempting to set 100 as the upper limit

// Import plugins
import qupath.tensorflow.stardist.StarDist2D
import static qupath.lib.gui.scripting.QPEx.*
//

// Specify whether the above model was trained using a single-channel image (e.g. IF DAPI)
// Get current image - assumed to have color deconvolution stains set
def imageData = getCurrentImageData()
def isBrightfield=imageData.isBrightfield()
def stains = imageData.getColorDeconvolutionStains() //will be null if IF

if (model_trained_on_single_channel!=1 && isBrightfield==false) {
    // If brightfield model but fluorescent image
    throw new Exception("Cannot use brightfield trained model to segment nuclei on IF image")
} else if (model_trained_on_single_channel == 1 && isBrightfield==true){
    //If fluorescent model but brightfield image (use deconvolution)
    println 'Performing detection on Brightfield image using single-channel trained model'
     stardist = StarDist2D.builder(pathModel)
            .preprocess(
                    ImageOps.Channels.deconvolve(stains),
                    ImageOps.Channels.extract(param_channel),
                    ImageOps.Filters.median(param_median),
                    ImageOps.Core.divide(param_divide),
                    ImageOps.Core.add(param_add)
            ) // Optional preprocessing (can chain multiple ops)

            .threshold(param_threshold)              // Prediction threshold
            .normalizePercentiles(normalize_low_pct,normalize_high_pct) // Percentile normalization
            .pixelSize(param_pixelsize)              // Resolution for detection
            .doLog()
            .includeProbability(true)
            .measureIntensity()
            .tileSize(param_tilesize)
            .measureShape()
            .cellExpansion(param_expansion) //Cell expansion in microns
            .constrainToParent(false)


             .build()
} else if (model_trained_on_single_channel == 1 && isBrightfield==false){
    //If IF model and IF image (no deconvolution preprocessing)
    println 'Performing detection on single channel image using single-channel trained model'

    stardist = StarDist2D.builder(pathModel)
            .preprocess(
                    ImageOps.Channels.extract(param_channel),
                    ImageOps.Filters.median(param_median),
                    ImageOps.Core.divide(param_divide),
                    ImageOps.Core.add(param_add)
            ) // Optional preprocessing (can chain multiple ops)

            .threshold(param_threshold)              // Prediction threshold
            .normalizePercentiles(normalize_low_pct,normalize_high_pct) // Percentile normalization. REQUIRED FOR IMC DATA
            .pixelSize(param_pixelsize)              // Resolution for detection
            .doLog()
            .includeProbability(true)
            .measureIntensity()
            .tileSize(param_tilesize)
            .measureShape()
            .cellExpansion(param_expansion)
            .constrainToParent(false)
            .build()
} else if (model_trained_on_single_channel == 0 && isBrightfield==true) {
    // If brightfield model and brightfield image
    println 'Performing detection on brightfield image using brightfield trained model'

    stardist = StarDist2D.builder(pathModel)
            .preprocess(
                    ImageOps.Filters.median(param_median),
                    ImageOps.Core.divide(param_divide),
                    ImageOps.Core.add(param_add)
            ) // Optional preprocessing (can chain multiple ops)

            .threshold(param_threshold)              // Prediction threshold
            .normalizePercentiles(normalize_low_pct,normalize_high_pct) // Percentile normalization. REQUIRED FOR IMC DATA
            .pixelSize(param_pixelsize)              // Resolution for detection
            .doLog()
            .includeProbability(true)
            .measureIntensity()
            .tileSize(param_tilesize)
            .measureShape()
            .cellExpansion(param_expansion)
            .constrainToParent(false)
            .build()

}
//Run stardist in selected annotation
def pathObjects = getSelectedObjects()
print(pathObjects)
if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
    return
}
clearDetections()
stardist.detectObjects(imageData, pathObjects)


//filter out small and low intensity nuclei


def toDelete = getDetectionObjects().findAll {measurement(it, nuc_area_measurement) <= min_nuc_area}
removeObjects(toDelete, true)
def toDelete2 = getDetectionObjects().findAll {measurement(it, nuc_intensity_measurement) <= min_nuc_intensity}
removeObjects(toDelete2, true)

println ('Done!')