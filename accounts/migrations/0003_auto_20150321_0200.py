# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime
from django.utils.timezone import utc


class Migration(migrations.Migration):

    dependencies = [
        ('accounts', '0002_auto_20150319_0503'),
    ]

    operations = [
        migrations.AlterField(
            model_name='userprofile',
            name='create_date',
            field=models.DateTimeField(default=datetime.datetime(2015, 3, 21, 2, 0, 27, 743165, tzinfo=utc), verbose_name='Date User Created'),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='userprofile',
            name='update_date',
            field=models.DateTimeField(default=datetime.datetime(2015, 3, 21, 2, 0, 27, 743424, tzinfo=utc), verbose_name='Date User Modified'),
            preserve_default=True,
        ),
    ]
