require 'ripper'
require 'pp'

class Project
  def initialize(files)
    @files = files
    @idents = []
  end

  def parse
    @files.each do |file|
      File.open(file, 'r') do |fp|
        finder = IdentFinder.new(fp, file)
        finder.parse
        @idents += finder.idents
      end
    end
  end

  def create_grammar(output_file)
    frequency = Hash.new(0)
    @idents.each do |ident|
      frequency[ident] += 1
    end
    File.open(output_file, 'w') do |f|
      frequency.each do |ident, count|
        f.puts("#{ident}\t#{count}")
      end
    end
  end
end

class IdentFinder < Ripper::Filter
  attr_reader :idents

  def initialize(src, filename = '-', lineno = 1)
    super
    @idents = []
  end

  %w(ident ivar const label cvar gvar).each do |event|
    define_method("on_#{event}") do |token, data|
      @idents << token.strip.gsub(/\A\./, '').gsub(':', '')
    end
  end
end

root = ARGV[0]
output = ARGV[1]

unless root && output
  STDERR.puts "Usage: ruby_tokenizer.rb PROJECT_ROOT OUTPUT_FILE"
  exit 1
end

files =
  if FileTest.file?(root)
    [root]
  else
    Dir.chdir(root) do
      `git ls-files | grep "\\.rb$"`.split("\n").map { |f| File.join(root, f) }
    end
  end

project = Project.new(files)
project.parse
project.create_grammar(output)
