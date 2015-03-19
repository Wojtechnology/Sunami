from django.contrib import admin
from accounts.models import UserProfile

# Register your models here.
class UserProfileAdmin(admin.ModelAdmin):
	fieldsets = [
		(None, {'fields' : ['user', 'status', 'display_picture',]})
	]

admin.site.register(UserProfile, UserProfileAdmin)