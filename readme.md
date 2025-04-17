# PrefixPacker 

## Description
PrefixPacker is a dictionary-based text encoding tool that encodes and decodes plain text files using a user-defined mapping dictionary from the command line. Based from a DSA Assignment
## To Run
From command line in the target directory:
```bash!
java -jar PrefixPacker.jar
```
:::    danger
*Best run on **PowerShell** or **Windows Terminal**. Limited support on standard CMD; untested on other OSs.*
:::
You’ll be prompted to select:
* Input file
* Output file *(**out.txt** by default)*
* Mapping file

The mapping file must be encoded as follows:
```csvpreview!
two,524
@@igh,526
..., ...
```

## Features
<div class="row">
    <div class="col-md-6">
      <h4>&emsp;Encoder</h4>
      <ul>
        <li>Transforms plain text into compact encoded representations</li>
        <li>Handles unknown words through UTF-8 hex encoding</li>
      </ul>
    </div>
    <div class="col-md-6">
      <h4>&emsp;Decoder</h4>
      <ul>
        <li>Handles both dictionary codes and hex-encoded content</li>
          <li>Ensures files are always consistent between operations</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-md-6">
      <h4>&emsp;Settings Management</h4>
      <ul>
        <li>Stores persistent configurations</li>
        <li>Contains Custom workflow options</li>
      </ul>
    </div>
    <div class="col-md-6">
      <h4>&emsp;File Management</h4>
      <ul>
        <li>CLI selection for dictionaries and text files</li>
        <li>Real-time directory scanning with browsing</li>
      </ul>
    </div>
  </div>
  
## Analysis from a DSA Perspective
:::success
<i>The application includes comments on Time & Space Complexity.</i>
:::
As this is a DSA project, the focus was on keeping time complexity low, which did mean using more memory.

O(n) performance was the ideal goal wherever possible. True O(1) wasn’t realistic due to how file and text processing works.

Choosing efficient methods was key, as many involve nested loops. For example:
*For Example:*

Using String Concatation:
```java
String result = "";
for (String word : words) {
     result += word; // O(n²) - Creates a new string each time
}
```

Example of using StringBuilder ():
```java
StringBuilder result = new StringBuilder();
for (String word : words) {
     result.append(word); // O(n) - uses a buffer
}
```
The encoder runs at O(n*L + m + i) as the modifed sliding window contains a loop. While this ***can*** be optimised with a new data structure, I found it became  complicated too quickly.

The decoder is faster at O(n + m + i) and avoids nested loops entirely.

> N: Number of words,
> 
> L: Max word length,
> 
> M: Map Size,
> 
> I: Input file size.

         
