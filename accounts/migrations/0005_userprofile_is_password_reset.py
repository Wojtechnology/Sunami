# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('accounts', '0004_auto_20150324_0147'),
    ]

    operations = [
        migrations.AddField(
            model_name='userprofile',
            name='is_password_reset',
            field=models.BooleanField(default=False, verbose_name='Password Reset Active'),
            preserve_default=True,
        ),
    ]
