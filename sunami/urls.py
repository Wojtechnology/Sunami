from django.conf.urls import patterns, include, url
from django.conf import settings
from django.conf.urls.static import static
from django.contrib import admin
from sunami.views import IndexView

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'sunami.views.home', name='home'),
    # url(r'^blog/', include('blog.urls')),

    url(r'^admin/', include(admin.site.urls)),
    url(r'^profile/', include('accounts.urls', namespace = 'accounts')),
    url(r'^$', IndexView.as_view(), name = 'index'),
 
) + static(settings.MEDIA_URL, document_root = settings.MEDIA_ROOT)