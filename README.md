## README

STATUS: Pre-alpha, in design and prototyping phase.

### About

`tape.toasts`

A Toast is a non-modal, unobtrusive window element used to display brief,
auto-expiring windows of information to a user. 

### Install

```clojure
{:deps {tape/toasts {:local/root "../toasts"}}}
```

### Usage

#### List toasts

Add toasts listing to a layout view via `toasts.v/index` partial:

```clojure
(ns my.app.layout.view
  (:require [tape.toasts.view :as toasts.v]))

(defn layout []
  [:div
    ...
    [toasts.v/index]])
```

The style is based on [Bulma](https://bulma.io/), but you can use your own
style based on it's classes or make your own partial for listing.

#### Create a toast

```clojure
(ns my.app.some.controller
  (:require [re-frame.core :as rf]
            [tape.toasts.controller :as toasts.c]))

(rf/dispatch [::toasts.c/create :info "Some message"])
;; Toast kind can be one of: :success, :danger, :warning, :info
```

#### License

Copyright © 2019 clyfe

Distributed under the MIT license.
