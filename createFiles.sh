counter=1
while [[ $counter -le 100 ]]; do
  touch ./files/$counter.txt
  counter=$((counter+1))
done
