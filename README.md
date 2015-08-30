# Panop [![Build Status](https://travis-ci.org/mdemarne/panop-core.svg)](https://travis-ci.org/mdemarne/panop-core)
Simple Tool For Parallel Online Search

## About
Some internal search systems for website are simply not accurate enough.
Often, external indexed-based search tools are not sufficient either,
especially when the data is moving fast, or only specific subsets of websites
are targeted.

One way to avoid this is to do online analysis and search: this is what 
Panop is for.

Panop will provide you a simple interface that allow you to select the target
of a search and its settings, as well as domain boundaries and search
depth. It will then return you a list of URLs matching your requirements
and allow you to browse through it.

Panop is a command-line-based tool.

## How to use
Panop is coded in Scala and uses SBT. To compile and use Panop, please clone 
this repository. Execute:
```
sbt publishLocal
```
and then:
```
sbt script
```
...to generate a script at the root of the project. You can then move the 
script anywhere in your system (e.g. in your bash path). To learn how to use 
Panop, simply run:
```
panop --help
```

## Note
The use of Panop can produce some workload on websites, depending of 
its settings.

## TODO

- Allow more complicated query (with exclusions)
- Allow more than one domain reach and launch on multiple pages
- Move to have UI printouts only in ui
- Do web interface using Play