# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime
from django.utils.timezone import utc


class Migration(migrations.Migration):

    dependencies = [
        ('accounts', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='userprofile',
            name='create_date',
            field=models.DateTimeField(default=datetime.datetime(2015, 3, 19, 5, 3, 21, 251470, tzinfo=utc), verbose_name='Date User Created'),
            preserve_default=True,
        ),
        migrations.AddField(
            model_name='userprofile',
            name='update_date',
            field=models.DateTimeField(default=datetime.datetime(2015, 3, 19, 5, 3, 21, 251714, tzinfo=utc), verbose_name='Date User Modified'),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='userprofile',
            name='display_picture',
            field=models.ImageField(upload_to='display_pictures', blank=True, verbose_name='User Display Picture'),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='userprofile',
            name='status',
            field=models.CharField(blank=True, verbose_name='User Status', max_length=500),
            preserve_default=True,
        ),
    ]
