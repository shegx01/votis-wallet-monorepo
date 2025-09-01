import SwiftUI

@available(iOS 14.0, *)
struct LaunchScreenView: View {
    var body: some View {
        ZStack {
            // Background color that adapts to light/dark mode
            Color(.systemBackground)
                .ignoresSafeArea()
            
            // Centered logo
            Image("LaunchImage")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 100, height: 100)
        }
    }
}

@available(iOS 14.0, *)
struct LaunchScreenView_Previews: PreviewProvider {
    static var previews: some View {
        LaunchScreenView()
    }
}
