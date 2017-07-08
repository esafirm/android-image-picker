## Changelog

**1.7.4 - More fixes and improvement**

- Fix `returnAfterFirst` not working in gallery selection
- Add config checker

**1.7.3 - Bug fixes**

- Fix back button issue when `setFolderMode` set to true
- Expose `ImagePickerConfig` in `ImagePicker`. Now you can override `getConfig()` before called by `getIntent()`

**1.7.2 - Internal changes & bugfix**

- Fix `Snackbar` issue when permission not granted
- Add toggle log feature

```
ImagePicker.enableLog(false)
```

**1.6.0 - Custom ImageLoader**

- Adding custom image loader
- Removing traditional `Intent` starter
