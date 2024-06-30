curl -X POST http://localhost:8080/upload -F "filename=@/Users/akshitkhatri/Documents/OReilly/datastructures.pdf"#To handle file uploads correctly and parse multipart form data,
# we need to follow the multipart/form-data specification more closely.
# In multipart form data, the filename is typically specified in the Content-Disposition header
# within each part of the body.

curl -v -X POST "http://localhost:8080/download?filename=test.txt"

curl -X POST -H "Content-Type: application/json" -d '{"name":"Item1","description":"This is item 1"}' http://localhost:8080/items
