lazy val root = (project in file(".")).enablePlugins(SbtWeb)

includeFilter in (Assets, StylusKeys.stylus) := "main.styl" | "inline.styl"

StylusKeys.useNib in Assets := true

StylusKeys.includeCSS in Assets := true

StylusKeys.inlineImages in Assets := true

StylusKeys.inlineFunction in Assets := "data-uri"

// StylusKeys.inlineThreshold in Assets := 10000

StylusKeys.inlineThreshold in Assets := 25000

StylusKeys.sourceMap in Assets := true

// StylusKeys.sourceMapInline in Assets := true

// StylusKeys.sourceMapRoot in Assets := "testRoot"

StylusKeys.compress in Assets := true