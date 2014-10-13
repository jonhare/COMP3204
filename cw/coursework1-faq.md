---
layout: index
rdir: "../"
title: "COMP3204 Computer Vision"
subtitle: "Coursework 1 FAQ & Problem Solving"
githubHeader: "false"
credits: Maintained by <a href="http://www.ecs.soton.ac.uk/people/msn">Professor Mark Nixon</a> and <a href="http://www.ecs.soton.ac.uk/people/jsh2">Dr Jonathon Hare</a>.
---

Let us know of any problems you encounter issues or have questions & we'll add them to the list below:

##Problems with Maven:
###Error building pom:
If you get a error message saying there was an error building the pom that looks like this:
	
	...
	...
	...
	[INFO] ------------------------------------------------------------------------
	[ERROR] BUILD ERROR
	[INFO] ------------------------------------------------------------------------
	[INFO] Error building POM (may not be this project's POM).


	Project ID: null:opennlp-tools:bundle:null

	Reason: Cannot find parent: org.apache.opennlp:opennlp for project: null:opennlp-tools:bundle:null for project null:opennlp-tools:bundle:null


	[INFO] ------------------------------------------------------------------------
	[INFO] For more information, run Maven with the -e switch
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 4 seconds
	[INFO] Finished at: Fri Oct 03 13:56:37 BST 2014
	[INFO] Final Memory: 39M/445M
	[INFO] ------------------------------------------------------------------------

Then edit the pom.xml file in a text editor and comment out the xml block for the `common-stream` dependency. Maven should then run without issue. We're actively looking into why this happens (it doesn't happen on all machines, but was observed on a Zepler lab linux box).

###"mvn assembly:assembly" is really slow.
We think this might be to do with the networked file-system housing your home areas on the Zepler lab machines. On a local disk, it runs within a few 10's of seconds. Fortunately, you don't need to do this often as you can run the code directly from Eclipse (or your IDE of choice) and so hopefully won't be too much of a problem in the long-run.

