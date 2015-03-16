from django.shortcuts import render, redirect
from django.views.generic import View

# Class to display front page
class IndexView(View):
	def get(self, request):

		context = {'page-title': ''}

		return render(request, 'index.html')

# Class to display input page
class LoginView(View):
	def get(self, request):
		return render(request, 'login.html')

	def post(self, request):
		return redirect('index')

# Class to display signup page
class SignupView(View):
	def get(self, request):
		return render(request, 'signup.html')

	def post(self, request):
		return redirect('index')