from django.conf.urls import patterns, url
from accounts.views import SignUpView, LoginView, LogoutView, ConfirmView, PasswordResetView, IForgotView

urlpatterns = patterns('',

    url(r'^login/$', LoginView.as_view(), name = 'login'),
    url(r'^signup/$', SignUpView.as_view(), name = 'signup'),
    url(r'^logout/$', LogoutView.as_view(), name = 'logout'),
    url(r'^iforgot/$', IForgotView.as_view(), name = 'iforgot'),
    url(r'^confirm/(?P<confirmation_code>[a-zA-Z0-9]+)/(?P<username>[a-zA-Z0-9]+)/$', ConfirmView.as_view(), name = 'confirm'),
    url(r'^passreset/(?P<confirmation_code>[a-zA-Z0-9]+)/(?P<username>[a-zA-Z0-9]+)/$', PasswordResetView.as_view(), name = 'passreset'),
	
	)