curl -X POST http://localhost:8080/upload -F "filename=@/Users/akshitkhatri/Documents/OReilly/datastructures.pdf"#To handle file uploads correctly and parse multipart form data,
# we need to follow the multipart/form-data specification more closely.
# In multipart form data, the filename is typically specified in the Content-Disposition header
# within each part of the body.

curl -v -X POST "http://localhost:8080/download?filename=test.txt"

curl -X POST -H "Content-Type: application/json" -d '{"name":"Item1","description":"This is item 1"}' http://localhost:8080/items

curl -X GET http://localhost:8080/items/1

curl -X  PUT http://localhost:8080/testfile.txt -d "This is the new updated content"

curl -X PUT -H "Content-Type: application/json" -d '{"name":"Updated Item1","description":"This is the updated item 1"}' http://localhost:8080/items/1

curl -v -X DELETE http://localhost:8080/testfile.txt

curl -X DELETE http://localhost:8080/items/1