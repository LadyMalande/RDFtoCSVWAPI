<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a id="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->

<!-- This md template has been copied from https://github.com/othneildrew/Best-README-Template -->

<!-- PROJECT LOGO -->
<br />
<div>


<h3 align="center">RDFtoCSVWAPI</h3>

  <p align="center"> 
    RDF to CSVW data converter web service
    <br />
    <a href="https://github.com/LadyMalande/RDFtoCSV"><strong>Explore the underlying library RDFtoCSV »</strong></a>
    <br />
    <br />
    <a href="https://rdf-to-csvw.onrender.com/swagger-ui/index.html">View Live Instance</a>
    ·
   </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project
<div id=“about-the-project”></div>
RDFtoCSVWAPI is a web service built on the RDFtoCSV library. It is a part of thesis.

It allows users to convert RDF files to CSV on the Web (CSVW). Or just its part (CSV/metadata JSON).

To try out live web service, go to: [Swagger UI RDFtoCSV](https://rdf-to-csvw.onrender.com/swagger-ui/index.html).


<p>(<a href="#readme-top">back to top</a>)</p>



### Built With
<div id=“built-with”></div>
The web service is ready to deploy with Dockerfile or run locally on docker.

There is Swagger UI generator in the app that creates easy to use UI for users.

The CORS setting in this time in WebConfig only allows local requests and requests from the web application built upon this live web service instance.

* [![Next][Java]][Java-url]
* Maven

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started


### Prerequisites

Have these installed:
* Docker (Linux)
* Docker Desktop (Windows)
* Git

### Installation for use

Prerequisites are having Maven and Java 17 or 19 on your device.

1. (For Windows) Open Docker Desktop

2. Clone the repo
   ```sh
   git clone https://github.com/LadyMalande/RDFtoCSVWAPI.git
   ```
3. Open command line and navigate to the directory containing the cloned project RDFtoCSVWAPI

4. Enter this to your command line to build docker image. It can take several minutes.:
   ```sh
   docker -t rdf-to-csv-api .
    ```
5. Check that you see rdf-to-csv-api in the list of docker images after inputting this to command line:
   ```sh
    docker images
   ```
6. Run the image with this:
    ```sh
    docker run -p 8080:8080 rdf-to-csv-api
   ```
7. You should see a logo of Spring in your terminal right now.
8. If you open your web browser, you can try checking out the web service running on http://localhost:8080/.
    The Web page should greet you with a simple text.
9. To navigate to the Swagger UI, go to this URL: http://localhost:8080/swagger-ui/index.html
<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Usage
Available methods:
  <a href="https://rdf-to-csvw.onrender.com/swagger-ui/index.html">
    <img src="images/swaggerUIRDFtoCSVWAPI.png" alt="Swagger UI" >
  </a>

Parameters shared among all methods:

* table (optional): choice of ONE of MORE tables to be made. DEFAULT: ONE
* conversionMethod (optional): choice of RDF4J, STREAMING and BIGFILESTREAMING. More about these methods in library RDFtoCSV documentation. DEFAULT: RDF4J.
* firstNormalForm (optional): true/false. If true, if a cell would contain a list of values, it is instead made into multiple lines in the CSV. Each cell contains only atomic value. DEFAULT: false.

Parameters for GET:
* url (required): a URL of an RDF file for conversion

Parameters for POST: 
* file (required): a file object. For trying out the POST methods with a file, it is recommended to use some kind of UI, for example Postman, to create the correct file representation for the user when sending the request.

<a href="https://rdf-to-csvw.onrender.com/swagger-ui/index.html">
<img src="images/GETparameters.png" alt="GET parameters" >
</a>

Project Link for this web service: [https://github.com/LadyMalande/RDFtoCSVWAPI](https://github.com/LadyMalande/RDFtoCSVWAPI)

Project link for web application using this web service: [https://github.com/LadyMalande/rdf-to-csv.github.io](https://github.com/LadyMalande/rdf-to-csv.github.io)


<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Using cURL
To call the web service, it is also possible to use cURL. You can either use one of your own making or get one generated 
when using Swagger UI.

<a href="https://rdf-to-csvw.onrender.com/swagger-ui/index.html">
<img src="images/onlycURL.png" alt="curl usage in cmd line" >
</a>

Generated cURL is located under the parameters of method. You need to first click on Try it out, fill in the desired parameters 
and then click on Execute. Then the cURL is generated:
```sh
curl -X 'GET' 'http://localhost:8080/csv?url=https%3A%2F%2Fw3c.github.io%2Fcsvw%2Ftests%2Ftest005.ttl' -H 'accept: application/octet-stream'
```

If you use Windows, you need to make a slight adjustments to the generated cURL (delete single quotation marks around GET and 
change single quotations marks for double quotation marks elsewhere):

```sh
curl -X GET "http://localhost:8080/csv?url=https%3A%2F%2Fw3c.github.io%2Fcsvw%2Ftests%2Ftest005.ttl" -H "accept: application/octet-stream"
```
When you use the cURL, you will see the returned CSV string:

<a href="https://rdf-to-csvw.onrender.com/swagger-ui/index.html">
<img src="images/curlUsedInCMDLineOnWindowsCSV.png" alt="curl usage in cmd line" >
</a>

To actually get the command line to download the incoming response as a proper file, you need to tweak the cURL more:
```sh
curl -X GET "http://localhost:8080/csv?url=https%3A%2F%2Fw3c.github.io%2Fcsvw%2Ftests%2Ftest005.ttl" -H "accept: application/octet-stream" -o simpsons.csv
```
<a href="https://rdf-to-csvw.onrender.com/swagger-ui/index.html">
<img src="images/curlToDownloadTheFile.png" alt="curl usage in cmd line download" >
</a>

After this the fetched CSV is saved as "simpsons.csv" in the same active directory as the command was executed.




<!-- CONTACT -->
## Contact

Tereza Miklóšová

Project Link for RDFtoCSV library depended in this project: [https://github.com/LadyMalande/RDFtoCSV](https://github.com/LadyMalande/RDFtoCSV)

<p align="right">(<a href="#readme-top">back to top</a>)</p>




<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/tereza-miklosova/
[Java]: https://img.shields.io/badge/Java-%23ED8B00.svg?logo=openjdk&logoColor=white
[Java-url]: https://www.java.com/en/

This md template has been copied from [https://github.com/othneildrew/Best-README-Template]