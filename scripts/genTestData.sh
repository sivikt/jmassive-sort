#!/bin/sh

echo "generate 100 numbers"
groovy ../../scripts/generateInput.groovy 100 testSrc/resources/autogen/inputTiny.txt

echo "generate 2000 numbers"
groovy ../../scripts/generateInput.groovy 2000 testSrc/resources/autogen/inputSmall.txt

echo "generate 40000 numbers"
groovy ../../scripts/generateInput.groovy 40000 testSrc/resources/autogen/inputMedium.txt

echo "generate 800000 numbers"
groovy ../../scripts/generateInput.groovy 800000 testSrc/resources/autogen/inputBig.txt

echo "generate 16000000 numbers"
groovy ../../scripts/generateInput.groovy 16000000 testSrc/resources/autogen/inputLarge.txt

echo "generate 320000000 numbers"
groovy ../../scripts/generateInput.groovy 320000000 testSrc/resources/autogen/inputHuge.txt
