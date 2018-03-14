counter=1
while [[ $counter -le 100 ]]; do
  touch ./files/$counter.txt
  echo "this\nis\ntext\nfile: "$counter.txt > ./files/$counter.txt
  counter=$((counter+1))
done
