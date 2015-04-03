from django.shortcuts import render, redirect
from django.views.generic import View

# Class to display front page
class IndexView(View):
	def get(self, request):
		return render(request, 'index.html', {'page_title' : 'Sunami - The Global Music Community'})