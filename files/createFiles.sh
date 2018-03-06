counter=1
while [[ $counter -le 100 ]]; do
  touch $counter.txt
  counter=$((counter+1))
done
