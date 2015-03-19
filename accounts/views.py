from django.shortcuts import render
from django.contrib.auth import authenticate, login
from django.views.generic import View
from django.utils import timezone
from django.contrib.auth.models import User
from accounts.models import UserProfile
from accounts.forms import UserForm

# Class to login to user profile
# Write tests for this
class LoginView(View):

	def get(self, request):
		return render(request, 'accounts/login.html')

	def post(self, request):

		errors = []

		email = request.POST.get('email')
		password = request.POST.get('password')

		# Find username based on email address given
		username = User.objects.filter(email = email)[0].username

		user = authenticate(username = username, password = password)

		# If user could be found, login user
		if user:

			# If user is active, send back to homepage logged in
			if user.is_active:
				login(request, user)

			else:
				errors.append('Account is inactive')

		else:
			errors.append('Password does not match Email')

		return render(request, 'accounts/login.html', {'errors' : errors})


# Class to register a new user profile
# Write tests for this
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