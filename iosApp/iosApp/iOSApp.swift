import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinIosKt.startKoinForIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
