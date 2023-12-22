### Input data
A data set is stored in a text file, with the first line serving as the title of the diagram and the second line as label of the source. Following these two lines, there 
are a variable number of additional lines, each containing the name of a category along with the corresponding value.
"data.txt" is an example.

### JDK and JavaFX
jdk-16.0.2
javaFX 16

### Classes
##### 0. Main
Main gui Application Object.

##### 1. MyData
Maintains String and Double variables for the information of the source and title, and a HashMap to store the data of the target categories.

##### 2. SankeyPane
Extends javafx.secene.layout.Pane.
Provide a function "display_data()" for stage to display Sankey diagram using a MyData object.

Private functions "svgOfRect()" and "svgOfBezierCurve()" are designed to return a StringBinding.
By this way the geometric components of the diagram (rectangles and curving streams) can be painted by SVGPath objects which is binding with the width/height properties of this pane object,
so the size of the total diagram can change with the GUI window simultaneously.
