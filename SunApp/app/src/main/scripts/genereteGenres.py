import json

oldFile = open('genresRaw.json', 'r')
genresObj = json.loads(oldFile.read())
print 'Loaded file'
oldFile.close()

keys = genresObj.keys()

newObj = []
for key in keys:
	genre = dict()
	genre['genre'] = key

	# Short term info
	genre['st'] = 0.0

	# Long term info
	genre['lt'] = 0.0

	# Associated genre
	genre['assoc'] = genresObj[key]

	newObj.append(genre)

print 'Created new object'

newFile = open('genres.json', 'w')
newFile.write(json.dumps(newObj))
print 'Wrote new file'
newFile.close()