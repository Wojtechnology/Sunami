from django.shortcuts import render
from django.views.generic import View

# Class to display front page
class IndexView(View):
	def get(self, request):
		return render(request, 'index.html')