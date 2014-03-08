#COMP3005 Interactive Demos, Slides and Handouts

This Github repository stores the lecture materials for part 2 of the [COMP3005 Computer Vision](https://secure.ecs.soton.ac.uk/module/COMP3005) course at the [University of Southampton](http://www.soton.ac.uk). The lectures presentations themselves are interactive slidesets created using [OpenIMAJ](http://www.openimaj.org).

From this page you can get the source-code for the presentations, which you can build yourself following the instructions below. If you just want to run the presentations for yourself, you can download the latest version of the pre-compiled runnable jar from [here](http://jenkins.ecs.soton.ac.uk/job/COMP3005/lastSuccessfulBuild/artifact/app/target/COMP3005-1.0-SNAPSHOT-jar-with-dependencies.jar). More details, as well as PDF versions of the handouts and slides are available [here](http://jonhare.github.io/COMP3005/).

##Building & running the code
You need to have [Apache Maven](http://maven.apache.org) installed to build the code (it should work with Maven 2 or Maven 3). Fork or clone the repository (or download a source [tarball](https://github.com/jonhare/COMP3005/tarball/master)/[zipball](https://github.com/jonhare/COMP3005/zipball/master)) & then from the command line navigate to the `app` directory within the source tree. Run `mvn install assembly:assembly` to build the presentation, and use `java -jar target/COMP3005-1.0-SNAPSHOT-jar-with-dependencies.jar` to launch the main interface.

