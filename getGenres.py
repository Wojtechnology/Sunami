import requests
import json

api_key = '3RPLB9Z6PQGWPK8A4'
url = 'http://developer.echonest.com/api/v4/genre'

genresList = []

res = requests.get(url + '/list?api_key=' + api_key + '&format=json')
data = json.loads(res.content)['response']
results = data['total']

res = requests.get(url + '/list?api_key=' + api_key + '&format=json&results=' + str(results))
genres = json.loads(res.content)['response']['genres']

for genre in genres:
	genresList.append(genre['name'])

for i in range(len(genresList)):
	res = requests.get(url + '/similar?api_key=' + api_key + '&format=json&name=' + genresList[i])
	rateLimit = res.headers['x-ratelimit-remaining']

	while(rateLimit == '0' or res.status_code != 200):
		res = requests.get(url + '/similar?api_key=' + api_key + '&format=json&name=' + genresList[i])
		rateLimit = res.headers['x-ratelimit-remaining']

	genres = json.loads(res.content)['response']['genres']

	print 'Genre: ' + genresList[i] + '\nrate-limit: ' + str(rateLimit)

	relatedGenres = []

	for genre in genres:
		relatedGenres.append(genre['name'])
		print '    ' + genre['name']