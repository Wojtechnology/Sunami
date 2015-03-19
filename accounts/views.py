from django.shortcuts import render
from django.views.generic import View
from django.utils import timezone
from accounts.models import UserProfile
from accounts.forms import UserForm

# Class to login to user profile
class LoginView(View):

	def get(self, request):
		return render(request, 'accounts/login.html')

#Class to register a new user profile
class SignUpView(View):

	def get(self, request):
		user_form = UserForm()

		return render(request, 'accounts/signup.html', {'user_form' : user_form, 'registered' : False})

	def post(self, request):
		registered = False

		user_form = UserForm(data = request.POST)

		if user_form.is_valid():
			user = user_form.save()

			# Required to hash the password
			user.set_password(user.password)
			user.save()

			# DateTimes automatically added in models.py
			profile = UserProfile(user = user)
			profile.save()

			registered = True

		else:
			print(user_form.errors)

		return render(request, 'accounts/signup.html', {'user_form' : user_form, 'registered' : registered})

