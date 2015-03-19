from django.shortcuts import render, redirect
from django.views.generic import View

# Class to display front page
class IndexView(View):
	def get(self, request):

		context = {'page-title': ''}

		return render(request, 'index.html')