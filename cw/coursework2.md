---
layout: index
rdir: "../"
title: "COMP3204 Computer Vision"
subtitle: "Coursework 2: Image Filtering and Hybrid Images"
githubHeader: "false"
credits: Maintained by <a href="http://www.ecs.soton.ac.uk/people/msn">Professor Mark Nixon</a> and <a href="http://www.ecs.soton.ac.uk/people/jsh2">Dr Jonathon Hare</a>. <br /> This coursework is based on the excellent <a href="http://cs.brown.edu/courses/cs143/proj1/">coursework for the CS143 Introduction to Computer Vision module</a> taught by James Hays at Brown University, but has been adapted to use OpenIMAJ.
---

## Brief
Due date: Friday 12th November, 16:00.  
Sample images: [hybrid-images.zip](./hybrid-images.zip)  
Code submission test tool: [SubmissionTester.jar](./SubmissionTester.jar)  
Handin: [2122/COMP3204/1/](https://handin.ecs.soton.ac.uk/handin/2122/COMP3204/1/)  
Required files: `MyConvolution.java`, `MyHybridImages.java`, `[hybridimage.png]`  
Credit: 10% of overall module mark  

## Overview
The goal of this assignment is to write a basic image convolution function and use it to create [hybrid images](http://web.archive.org/web/20150321184824/http://cvcl.mit.edu/hybridimage.htm) using a simplified version of the [SIGGRAPH 2006 paper by Oliva, Torralba, and Schyns](http://cvcl.mit.edu/publications/OlivaTorralb_Hybrid_Siggraph06.pdf). Hybrid images are static images that change in interpretation as a function of the viewing distance. The basic idea is that high frequency tends to dominate perception when it is available, but, at a distance, only the low frequency (smooth) part of the signal can be seen. By blending the high frequency portion of one image with the low-frequency portion of another, you get a hybrid image that leads to different interpretations at different distances. An example of a hybrid image is shown below.

<div style="text-align:center">
<img src="hybrid_image.jpg"/> <br />
Example hybrid image. Look at image from very close, then from far away.<br /><br />
</div>

## Details
This project is intended to familiarise you with image filtering and the implementation of a convolution function in OpenIMAJ. Once you have created an image convolution function, it is relatively straightforward to construct hybrid images. You will need to have worked through [Chapter 1](http://www.openimaj.org/tutorial/getting-started-with-openimaj-using-maven.html), [Chapter 2](http://www.openimaj.org/tutorial/processing-your-first-image.html) and [Chapter 7](http://www.openimaj.org/tutorial/processing-video.html) of the [OpenIMAJ tutorial](http://www.openimaj.org/tutorial/) prior to starting this coursework.

**Template convolution.** Template convolution is a fundamental image processing tool. Mark has covered convolution in detail in the lectures. See section 3.4.1 of Mark Nixon's book (Fourth Edition) by looking at [this excerpt](template_convolution_4th_edition.pdf) and the lecture materials for more information. 

OpenIMAJ has numerous built in and highly efficient operators to perform convolution, but you will be writing your own such function from scratch for this assignment. More specifically, you will implement a class called `MyConvolution` that builds on this skeleton:

	package uk.ac.soton.ecs.<your_username>.hybridimages;

	import org.openimaj.image.FImage;
	import org.openimaj.image.processor.SinglebandImageProcessor;

	public class MyConvolution implements SinglebandImageProcessor<Float, FImage> {
		private float[][] kernel;

		public MyConvolution(float[][] kernel) {
			//note that like the image pixels kernel is indexed by [row][column]
			this.kernel = kernel;
		}

		@Override
		public void processImage(FImage image) {
			// convolve image with kernel and store result back in image
			//
			// hint: use FImage#internalAssign(FImage) to set the contents
			// of your temporary buffer image to the image.
		}
	}

You will need to fill in the `processImage` method so that it performs convolution of the image with the kernel/template. Your implementation must support arbitrary shaped kernels, as long as both dimensions are odd (e.g. 7x9 kernels but not 4x5 kernels). You should utilise (_possibly implicit_) zero-padding of the input image to ensure that the output image retains the same size as the input image and that the kernel can reach into the image edges and corners.

Note that the code you write for template convolution is designed to work on grey-level images (`FImage`), however the images you will process in the next section are colour (`MBFImage`). Convolution of a colour image will be performed by separately convolving each of the colour bands with the same kernel. OpenIMAJ will automatically take care of this for you when you pass your `MyConvolution` instance to the `process` method of an `MBFImage`.

Make sure that you implement the convolution operator and not a different (but similar) operator. Check that your implementation works correctly for non-symmetric kernels. You can try to implement the convolution using the Fourier transform, however please note that the specification asks for __zero-padding__ of the image. The Fourier transform replicates to infinity by virtue of the sampling process, so you will have make allowances for this with appropriate padding and cropping.

**Hybrid Images.** A hybrid image is the sum of a low-pass filtered version of the one image and a high-pass filtered version of a second image. There is a free parameter, which can be tuned for each image pair, which controls *how much* high frequency to remove from the first image and how much low frequency to leave in the second image. This is called the "cutoff-frequency". In the paper it is suggested to use two cutoff-frequencies (one tuned for each image) and you are free to try that, as well. 

Low pass filtering (removing all the high frequencies) can be achieved by convolving the image with a Gaussian filter. The cutoff-frequency is controlled by changing the standard deviation, sigma, of the Gaussian filter used in constructing the hybrid images. You can use the `Gaussian2D.createKernelImage(size, sigma)` method to create a image of a 2D Gaussian, and use the `pixels` field of the resultant image to get the 2D float array required for your `MyConvolution` constructor. The `size` parameter of `Gaussian2D.createKernelImage(size, sigma)` controls the width and height of the filter in pixels. It is standard practice for this to be set as a function of the sigma value as follows:

	int size = (int) (8.0f * sigma + 1.0f); // (this implies the window is +/- 4 sigmas from the centre of the Gaussian)
	if (size % 2 == 0) size++; // size must be odd

High pass filtering (removing all the low frequencies) can be most easily achieved by subtracting a low-pass version of an image from itself.

You should implement your hybrid images functionality by completing the following code skeleton:

	package uk.ac.soton.ecs.<your_username>.hybridimages;

	import org.openimaj.image.MBFImage;

	public class MyHybridImages {
		/**
		 * Compute a hybrid image combining low-pass and high-pass filtered images
		 *
		 * @param lowImage
		 *            the image to which apply the low pass filter
		 * @param lowSigma
		 *            the standard deviation of the low-pass filter
		 * @param highImage
		 *            the image to which apply the high pass filter
		 * @param highSigma
		 *            the standard deviation of the low-pass component of computing the
		 *            high-pass filtered image
		 * @return the computed hybrid image
		 */
		public static MBFImage makeHybrid(MBFImage lowImage, float lowSigma, MBFImage highImage, float highSigma) {
			//implement your hybrid images functionality here. 
			//Your submitted code must contain this method, but you can add 
			//additional static methods or implement the functionality through
			//instance methods on the `MyHybridImages` class of which you can create 
			//an instance of here if you so wish.
			//Note that the input images are expected to have the same size, and the output
			//image will also have the same height & width as the inputs.
		}
	}

We have provided you with 5 pairs of aligned images (in the [hybrid-images.zip](./hybrid-images.zip) file) which can be merged reasonably well into hybrid images. The alignment is important because it affects the perceptual grouping (read the paper for details). We encourage you to create an additional example (e.g. change of expression, morph between different objects, change over time, etc.) for full marks (see below). See the [hybrid images project page](http://web.archive.org/web/20150321184824/http://cvcl.mit.edu/hybridimage.htm) for some inspiration.

For the example shown at the top of the page, the two original images look like this:

<div style="text-align:center">
<img src="dog.jpg" width="300"/> <img src="cat.jpg" width="300"/><br /><br />
</div>

The low-pass (blurred) and high-pass versions of these images look like this:

<div style="text-align:center">
<img src="low_frequencies.jpg" width="300"/> <img src="high_frequencies.jpg" width="300"/><br /><br />
</div>

The high frequency image is actually zero-mean with negative values so it is visualised by adding 0.5 to every pixel in each colour channel. In the resulting visualisation, bright values are positive and dark values are negative.

Adding the high and low frequencies together gives you the image at the top of this page. If you're having trouble seeing the multiple interpretations of the image, a useful way to visualise the effect is by progressively down-sampling the hybrid image as is done below:

<div style="text-align:center">
<img src="cat_hybrid_image_scales.jpg" width="646"/><br /><br />
</div>

The OpenIMAJ [Image#drawImage](http://openimaj.org/apidocs/org/openimaj/image/Image.html#drawImage(I,%20int,%20int)) methods can be used in combination with the image resizing functionality found in the [ResizeProcessor](http://openimaj.org/apidocs/org/openimaj/image/processing/resize/ResizeProcessor.html) or [BilinearInterpolation](http://openimaj.org/apidocs/org/openimaj/image/processing/resize/BilinearInterpolation.html) classes to construct such a visualisation.

### Restrictions
You can use the convolution functions built in to OpenIMAJ for testing (e.g. [FGaussianConvolve](http://openimaj.org/apidocs/org/openimaj/image/processing/convolution/FGaussianConvolve.html), [FConvolution](http://openimaj.org/apidocs/org/openimaj/image/processing/convolution/FConvolution.html), etc), but do not use them in your implementation. The provided `SubmissionTester.jar` tool will check for usage of these classes and display an error if you do try to use them.

### What to hand in
You are required to submit the following items to ECS Handin:

* Your `MyConvolution.java` file
* Your `MyHybridImages.java` file

For full marks, you also need to submit a hybrid image creation of your own (ideally with the progressive downsampling shown above). Details below.

## Marking and feedback
This coursework is primarily automatically marked by a program that compiles and runs your submitted java files with a number of different parameters. This software provides a grade out of 8 (split 4/4 between the convolution and hybrid images parts), and also generates written feedback as it runs. The remaining two marks are available if you upload a novel hybrid image of your own creation. We're looking for an image which clearly encapsulates and demonstrates the notions of a hybrid image formation with particular demonstration of the effects consistent with progressive downsampling. Full marks will only be awarded for images that are particularly creative, impressive or funny. A selection of the best images will be shown to the class during a feedback lecture, which will cover a broad range of lessons related to the coursework.

We have provided a tool in the `SubmissionTester.jar` jar file that is capable of performing some elementary tests on your java code (like checking that it compiles, that the `MyConvolution` class can be instantiated and that the required methods run. You can run the tool from the commandline (you'll need java>=1.8.0) using:

	java -jar SubmissionTester.jar path/to/MyConvolution.java path/to/MyHybridImages.java

The tool doesn't perform any tests to check your code actually works correctly however, so you should check this yourself before submission of the files to handin! Note that when your code runs, it is executed in a restricted sandbox environment, and will throw errors if you try to read or write files, or access the network.

Standard ECS late submission penalties apply.

## Useful links
* [The OpenIMAJ Tutorial](http://openimaj.org/tutorial)
* [The OpenIMAJ Javadocs](http://openimaj.org/apidocs/index.html)
* [SIGGRAPH Hybrid Images Paper](http://cvcl.mit.edu/publications/OlivaTorralb_Hybrid_Siggraph06.pdf)
* [The Hybrid Images project page](http://web.archive.org/web/20150321184824/http://cvcl.mit.edu/hybridimage.htm)

## Questions
If you have any problems/questions then [email](mailto:jsh2@ecs.soton.ac.uk) or speak to [Jon](http://ecs.soton.ac.uk/people/jsh2) in his office.

