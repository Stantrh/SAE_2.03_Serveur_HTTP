#!/bin/bash

idProcessusServeur=$(cat /var/run/myweb.pid)
sudo kill $idProcessusServeur
exit 0
