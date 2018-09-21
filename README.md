# AnaClock  [![Release](https://jitpack.io/v/talhahasanzia/anaclock.svg)](https://jitpack.io/#talhahasanzia/anaclock/0.1)  [![GitHub issues](https://img.shields.io/github/issues/talhahasanzia/anaclock.svg)](https://github.com/talhahasanzia/anaclock/issues)   [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
Analog clock skeleton for android.



## Release
Available Version:  [0.1](https://github.com/talhahasanzia/anaclock/releases/tag/0.1) on [jitpack.io](https://jitpack.io/#talhahasanzia/anaclock/0.1) 


## Library Source
[Jump to library source.](https://github.com/talhahasanzia/anaclock/tree/master/anaclocklib)

## Getting Started

### Adding the library

In your project level gradle, add:
```
    maven { url "http://jitpack.io" }
```

In your app level gradle **(4.0+)**, add:
```
    implementation 'com.github.talhahasanzia:anaclock:0.1'
```
for gradle versions **below 4.0** use:
```
    compile 'com.github.talhahasanzia:anaclock:0.1'
```
## Using in your project
- Based on View class of android, this AnaClock will serve as skeleton for building custom analog clocks, stop watches or timer views.
- If you want to use this view in its simplest form, just add the view in your xml and provide attributes for seconds, minutes and hours styling.
- If you want to create your own skin, you can extend this class to make your own drawings while reusing most of the logic.
- Currently drawing is based on simple lines.
- Most of the methods are protected, so to develop a skin over this you may extend this class and override methods.



## Contributing

- Contributions are welcomed as long as they dont break the code. Please create an issue and have a discussion before pull request.
- There is still WIP so dont hesitate to report issues or pull requests.
- Also, if you created a skin based on this library you can create a pull request and we will add it in official release.


## Hosting

Thanks to jitpack.io! Hosted at: https://jitpack.io/#talhahasanzia/anaclock/

## Authors

* **Talha** - *Initial work* - [@talhahasanzia](https://github.com/talhahasanzia)

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](https://github.com/talhahasanzia/anaclock/blob/master/LICENSE) file for details.

*Sources from Android and Android APIs are subject to the licensing terms as per Android Open Source Project (AOSP).*

## Acknowledgments

* Inspiration : At the moment Android does not provide much customization for Time widgets so this was my take on it.
* Great help from my mentor [@syedowaisali](https://github.com/syedowaisali)

