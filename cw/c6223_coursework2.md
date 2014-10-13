---
layout: index
rdir: "../"
title: "COMP6223 Computer Vision (MSc)"
subtitle: "Coursework 2: Subverting Face Detection"
githubHeader: "false"
credits: Maintained by <a href="http://www.ecs.soton.ac.uk/people/msn">Professor Mark Nixon</a> and <a href="http://www.ecs.soton.ac.uk/people/jsh2">Dr Jonathon Hare</a>.
---

**This coursework is only for students registered on the COMP6223 module.**

##Brief
Due date: Thursday 27th November, 16:00.
Software tool: 
Handin: [1415/COMP6223/1/](https://handin.ecs.soton.ac.uk/handin/1415/COMP6223/1/)
Required files: report.pdf  
Credit: 10% of overall module mark  

##Overview
Face detection algorithms such as the classic [Viola-Jones Haar Cascade algorithm](viola04ijcv.pdf) are trained to select weakly-discriminative features that are commonly present (or not present) in images for faces. For example, a common feature in images of faces is that the average intensity of a rectangular region that crosses the eyes in lighter than the region immediately above or below it (see the image in the next section below). 

The aim of this coursework is for you to explore how you might camouflage a face in order for it not to be detected by software, whilst still being recognisable as a face by humans. We have provided a software implementation of a Haar cascade detector that you can use for experiments.

##Details


<div style="text-align:center">
<img src="haar.jpg"/> <br />
Examples of common positive features from a Haar cascade face detector. Taken from <a href="https://www.flickr.com/photos/unavoidablegrain/6884354772">https://www.flickr.com/photos/unavoidablegrain/6884354772</a>.
<br /><br />
</div>

###The report


###What to hand in
You need to submit to ECS Handin the following items:

* The report (as a PDF document)

##Marking and feedback
Marks will be awarded for:

* Identifying a range of techniques to subvert the face detector implementation.
* Excellence of professionalism in implementation and reporting.
* Quality and contents of the report.

Standard ECS late submission penalties apply.

Individual feedback will be given covering the above points.

##Useful links
* [The Original Viola/Jones Conference paper](viola-cvpr-01.pdf)
* [Extended journal paper](viola04ijcv.pdf)
* [Wikipedia description of the algorithm](http://en.wikipedia.org/wiki/Viola–Jones_object_detection_framework)
* The actual detector data is the ["haarcascade_frontalface_alt2.xml"](https://github.com/Itseez/opencv/blob/master/data/haarcascades/haarcascade_frontalface_alt2.xml) file from the [OpenCV project](www.opencv.org). Note that we're not using it with OpenCV, but rather with [OpenIMAJ](www.openimaj.org). The detector is a "Tree-based 20x20 gentle adaboost frontal face detector", created by [Prof. Rainer Lienhart](http://www.lienhart.de/).
* The source-code for the detector software we've provided is [here]() 

##Questions
If you have any problems/questions then [email](mailto:jsh2@ecs.soton.ac.uk) or speak to [Jon](http://ecs.soton.ac.uk/people/jsh2), either in his office, or in one of the drop-in sessions in the Zepler labs we'll run during the course.

