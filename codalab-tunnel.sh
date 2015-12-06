#!/bin/bash
echo "Tunnelling to CodaLab"
ssh -f -Nn -L 12800:localhost:2800 -L 18000:localhost:8000 codalab
