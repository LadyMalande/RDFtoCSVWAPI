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
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![Unlicense License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">


<h3 align="center">RDFtoCSVWAPI</h3>

  <p align="center">
    RDF to CSVW data converter web service
    <br />
    <a href="https://github.com/LadyMalande/RDFtoCSV"><strong>Explore the docs »</strong></a>
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

RDFtoCSVWAPI is a web service built on the RDFtoCSV library. It is a part of thesis.

It allows users to convert RDF files to CSV on the Web (CSVW). Or just its part (CSV/metadata JSON).

To try out live web service, go to: [Swagger UI RDFtoCSV](https://rdf-to-csvw.onrender.com/swagger-ui/index.html).


<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Built With

The web service is ready to deploy with Dockerfile or run locally on docker.

There is Swagger UI generator in the app that creates easy to use UI for users.

The CORS setting in this time in WebConfig only allows local requests and requests from the web application built upon this live web service instance.

* [![Next][Java]][Java-url]
* Maven

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started


### Prerequisites

This is an example of how to list things you need to use the software and how to install them.
* npm
  ```sh
  npm install npm@latest -g
  ```

### Installation for use

Prerequisities are having Maven and Java 17 or 19 on your device.

1. Get a JAR of this project (either build this one or get one [here](https://github.com/LadyMalande/RDFtoCSV-JAR))

To build the project yourself:

1. Clone the repo
   ```sh
   git clone https://github.com/LadyMalande/RDFtoCSV.git
   ```
2. Install NPM packages
   ```sh
   npm install
   ```
3. Enter your API in `config.js`
   ```js
   const API_KEY = 'ENTER YOUR API';
   ```
4. Change git remote url to avoid accidental pushes to base project
   ```sh
   git remote set-url origin github_username/repo_name
   git remote -v # confirm the changes
   ```

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