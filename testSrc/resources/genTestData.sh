#!/bin/sh

echo "generate 100 numbers"
groovy ../../scripts/generateInput.groovy 100 inputTiny.txt

echo "generate 2000 numbers"
groovy ../../scripts/generateInput.groovy 2000 inputSmall.txt

echo "generate 40000 numbers"
groovy ../../scripts/generateInput.groovy 40000 inputMedium.txt

echo "generate 800000 numbers"
groovy ../../scripts/generateInput.groovy 800000 inputBig.txt

echo "generate 16000000 numbers"
groovy ../../scripts/generateInput.groovy 16000000 inputLarge.txt

echo "generate 320000000 numbers"
groovy ../../scripts/generateInput.groovy 320000000 inputHuge.txt
